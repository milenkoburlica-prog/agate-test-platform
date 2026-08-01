package at.co.svc.open.api.spec.downloader;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;



public class OpenApiDownloader {


    private final HttpClient client =
            HttpClient.newHttpClient();



    public Path download(
            String openApiUrl)
            throws Exception {


        Path targetDir =
                Path.of(
                    "generated"
                );


        Files.createDirectories(
                targetDir
        );



        /*
         * Download main OpenAPI file
         */
        Path yamlFile =
                targetDir.resolve(
                    "openapi.yaml"
                );


        downloadFile(
                openApiUrl,
                yamlFile
        );



        /*
         * Download relative refs
         */
        downloadCommonFiles(
                openApiUrl,
                targetDir
        );



        return yamlFile;

    }








    private void downloadCommonFiles(
            String openApiUrl,
            Path targetDir)
            throws Exception {


        URI uri =
                URI.create(openApiUrl);



        String base =
                uri.toString()
                .substring(
                    0,
                    uri.toString()
                    .lastIndexOf("/")
                );



        Path commonDir =
                targetDir.resolve(
                    "common-openapi-files"
                );


        Files.createDirectories(
                commonDir
        );



        /*
         * Known SVC common OpenAPI references
         */
        String[] commonFiles = {


            "svc-alive-v1.yaml"

        };



        for(String file : commonFiles) {


            String fileUrl =
                    base
                    +"/common-openapi-files/"
                    +file;



            Path target =
                    commonDir.resolve(
                        file
                    );



            try {


                downloadFile(
                        fileUrl,
                        target
                );


                ///System.out.println("Downloaded OpenAPI ref: " + file);


            }
            catch(Exception e) {


                /////System.out.println("Optional OpenAPI ref not found: " + file);


            }


        }


    }








    private void downloadFile(
            String url,
            Path target)
            throws Exception {



        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                            URI.create(url)
                        )
                        .GET()
                        .build();



        HttpResponse<byte[]> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );



        if(response.statusCode()!=200) {


            throw new IOException(
                "Cannot download "
                +url
                +" HTTP "
                +response.statusCode()
            );

        }



        Files.copy(
                response.body()
                        .length == 0
                        ?
                        Files.createTempFile(
                                "empty",
                                ".tmp"
                        )
                        :
                        writeTemp(
                                response.body()
                        ),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );


    }







    private Path writeTemp(
            byte[] data)
            throws IOException {


        Path tmp =
                Files.createTempFile(
                    "openapi",
                    ".tmp"
                );


        Files.write(
                tmp,
                data
        );


        return tmp;

    }


}