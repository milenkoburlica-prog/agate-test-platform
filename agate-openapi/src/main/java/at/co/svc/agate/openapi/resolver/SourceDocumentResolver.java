package at.co.svc.agate.openapi.resolver;

import java.net.URI;
import java.nio.file.Path;

public class SourceDocumentResolver {

    public String normalize(
            String sourceDocument) {

        if (sourceDocument == null ||
                sourceDocument.isBlank()) {

            return "";
        }

        if (isUrl(sourceDocument)) {

            return URI.create(
                    sourceDocument
            )
                    .normalize()
                    .toString();
        }

        return Path.of(
                sourceDocument
        )
                .toAbsolutePath()
                .normalize()
                .toString();
    }


    public String resolve(
            String sourceDocument,
            String referencedDocument) {

        if (referencedDocument == null ||
                referencedDocument.isBlank()) {

            return normalize(
                    sourceDocument
            );
        }

        if (isUrl(referencedDocument)) {

            return URI.create(
                    referencedDocument
            )
                    .normalize()
                    .toString();
        }

        if (isUrl(sourceDocument)) {

            URI baseUri =
                    URI.create(
                            sourceDocument
                    );

            return baseUri
                    .resolve(
                            referencedDocument
                    )
                    .normalize()
                    .toString();
        }

        Path referencedPath =
                Path.of(
                        referencedDocument
                );

        if (referencedPath.isAbsolute()) {

            return referencedPath
                    .normalize()
                    .toString();
        }

        Path sourcePath =
                Path.of(
                        sourceDocument
                )
                .toAbsolutePath()
                .normalize();

        Path parent =
                sourcePath.getParent();

        if (parent == null) {

            return referencedPath
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        }

        return parent
                .resolve(
                        referencedDocument
                )
                .normalize()
                .toString();
    }


    public boolean isUrl(
            String value) {

        if (value == null) {
            return false;
        }

        String normalized =
                value.toLowerCase();

        return normalized.startsWith(
                "http://"
        )
                ||
                normalized.startsWith(
                        "https://"
                );
    }
}