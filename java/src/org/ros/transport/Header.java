package org.ros.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

public class Header {

  private static final Log log = LogFactory.getLog(Header.class);

  private Header() {
    // Utility class.
  }

  public static Map<String, String> readHeader(InputStream inputStream) throws IOException {
    LittleEndianDataInputStream in = new LittleEndianDataInputStream(inputStream);
    int size = in.readInt();
    byte[] header = in.readByteArray(size);
    return decode(header);
  }

  public static void writeHeader(Map<String, String> header, OutputStream outputStream)
      throws IOException {
    byte[] buffer = encode(header);
    LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(outputStream);
    out.writeInt(buffer.length);
    out.write(buffer);
    out.flush();
  }

  @VisibleForTesting
  static Map<String, String> decode(byte[] header) throws IOException {
    LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(header));
    Map<String, String> result = Maps.newHashMap();
    int position = 0;
    while (position < header.length) {
      int fieldSize = in.readInt();
      position += 4;
      if (fieldSize == 0) {
        throw new IllegalStateException("Invalid 0 length handshake header field.");
      }
      if (position + fieldSize > header.length) {
        throw new IllegalStateException("Invalid line length handshake header field.");
      }
      String field = in.readAsciiString(fieldSize);
      position += field.length();
      if (field.indexOf("=") == -1) {
        throw new IllegalStateException("Invalid line in handshake header: [" + field + "]");
      }
      String[] keyAndValue = field.split("=");
      result.put(keyAndValue[0], keyAndValue[1]);
    }
    log.info("Decoded header: " + result);
    return result;
  }

  @VisibleForTesting
  static byte[] encode(Map<String, String> header) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(buffer);
    for (Entry<String, String> entry : header.entrySet()) {
      String field = entry.getKey() + "=" + entry.getValue();
      out.writeInt(field.length());
      out.writeBytes(field);
    }
    out.close();
    return buffer.toByteArray();
  }
}
