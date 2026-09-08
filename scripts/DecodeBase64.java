import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Small build-time utility used by the macOS packaging script.
 *
 * The project already requires Java 21, so using Java's Base64 decoder avoids
 * relying on platform-specific base64 command-line syntax.
 */
public final class DecodeBase64 {
    private DecodeBase64() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: DecodeBase64 <input.b64> <output>");
        }

        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);

        String encoded = Files.readString(input);
        byte[] decoded = Base64.getMimeDecoder().decode(encoded);
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.write(output, decoded);

        if (decoded.length < 8
                || decoded[0] != (byte) 0x89
                || decoded[1] != 0x50
                || decoded[2] != 0x4E
                || decoded[3] != 0x47
                || decoded[4] != 0x0D
                || decoded[5] != 0x0A
                || decoded[6] != 0x1A
                || decoded[7] != 0x0A) {
            Files.deleteIfExists(output);
            throw new IllegalStateException("Decoded icon asset is not a PNG file.");
        }
    }
}
