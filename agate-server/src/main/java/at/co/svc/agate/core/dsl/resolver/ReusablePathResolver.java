package at.co.svc.agate.core.dsl.resolver;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ReusablePathResolver {

    private ReusablePathResolver() {
    }

    public static String resolve(
            String command,
            String application) {

        Path base =
                Paths.get(
                        "data",
                        application
                );

        String normalizedCommand =
                toRelativePath(command);

        Path resolved =
                base.resolve(
                        normalizedCommand + ".yaml"
                ).normalize();

        return resolved.toString();
    }

    private static String toRelativePath(
            String command) {

        StringBuilder result =
                new StringBuilder();

        int i = 0;

        while (i < command.length()) {

            if (i + 1 < command.length()
                    && command.charAt(i) == '.'
                    && command.charAt(i + 1) == '.') {

                if (result.length() > 0
                        && result.charAt(result.length() - 1)
                                != '/') {

                    result.append('/');
                }

                result.append("..");
                result.append('/');

                i += 2;

            } else if (command.charAt(i) == '.') {

                if (result.length() == 0) {
                    // leading single dot = current base
                    i++;
                    continue;
                }

                result.append('/');
                i++;

            } else {

                result.append(command.charAt(i));
                i++;
            }
        }

        return result.toString();
    }
}