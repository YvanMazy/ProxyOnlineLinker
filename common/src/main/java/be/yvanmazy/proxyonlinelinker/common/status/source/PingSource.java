/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package be.yvanmazy.proxyonlinelinker.common.status.source;

import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PingSource implements StatusSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingSource.class);
    private static final byte[] STATUS_REQUEST_PACKET = {0x00};

    private final String host;
    private final int port;
    private final int timeout;
    private final int protocol;
    private final Proxy proxy;

    private final byte[] handshakeDataPacket;

    public PingSource(final @NotNull String host, final int port, final int timeout, final int protocol, final @Nullable Proxy proxy) {
        this.host = Objects.requireNonNull(host, "host must not be null");
        this.port = Preconditions.requirePort(port);
        this.timeout = Math.max(timeout, 0);
        this.protocol = protocol;
        this.proxy = proxy;

        try {
            this.handshakeDataPacket = this.buildHandshakePacket();
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to build handshake packet", e);
        }
    }

    @Override
    public int fetch() {
        try (final Socket socket = this.openSocket()) {
            // Connect to the target server
            socket.setSoTimeout(this.timeout);
            socket.connect(new InetSocketAddress(this.host, this.port));

            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            final DataInputStream in = new DataInputStream(socket.getInputStream());

            writePacket(out, this.handshakeDataPacket);

            writePacket(out, STATUS_REQUEST_PACKET); // Status request

            readVarInt(in); // Read packet length
            final int packetId = readVarInt(in);
            if (packetId != 0x00) { // Expected packet ID
                this.warnError("Unexpected packet ID=" + packetId);
                return -1;
            }

            final int jsonLength = readVarInt(in); // TODO: Check length validity
            final byte[] jsonBytes = in.readNBytes(jsonLength);
            final String json = new String(jsonBytes, StandardCharsets.UTF_8);

            final String needle = "\"online\":";
            int idx = json.indexOf(needle);
            if (idx < 0) {
                return -1;
            }
            idx += needle.length();
            int end = json.indexOf(',', idx);
            if (end < 0) {
                end = json.indexOf('}', idx);
            }
            return Integer.parseInt(json.substring(idx, end).trim());
        } catch (final IOException | NumberFormatException e) {
            this.warnError("Exception=" + e.getMessage());
            return -1;
        }
    }

    @Override
    public @NotNull StatusSourceType type() {
        return StatusSourceType.PING;
    }

    private void warnError(final String message) {
        LOGGER.warn("Failed to ping: {}", message);
    }

    private byte[] buildHandshakePacket() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writeVarInt(stream, 0x00); // Packet ID
        writeVarInt(stream, this.protocol); // Protocol ID
        writeString(stream, this.host); // Write host
        stream.write((this.port >>> 8) & 0xFF); // Port high‑byte
        stream.write(this.port & 0xFF); // Port low‑byte
        writeVarInt(stream, 1); // Next state = status
        return stream.toByteArray();
    }

    private Socket openSocket() {
        if (this.proxy != null) {
            return new Socket(this.proxy);
        }
        return new Socket();
    }

    private static void writePacket(final DataOutputStream out, final byte[] payload) throws IOException {
        writeVarInt(out, payload.length);  // Length prefix
        out.write(payload);
    }

    private static void writeString(final OutputStream out, final String s) throws IOException {
        final byte[] data = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, data.length);
        out.write(data);
    }

    private static void writeVarInt(final OutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value);
    }

    private static void writeVarInt(final DataOutputStream out, final int value) throws IOException {
        writeVarInt((OutputStream) out, value);
    }

    private static int readVarInt(final InputStream in) throws IOException {
        int numRead = 0, result = 0, read;
        do {
            read = in.read();
            if (read == -1) {
                throw new EOFException("VarInt truncated");
            }
            result |= (read & 0x7F) << (7 * numRead++);
            if (numRead > 5) {
                throw new IOException("VarInt too long");
            }
        } while ((read & 0x80) != 0);
        return result;
    }

}