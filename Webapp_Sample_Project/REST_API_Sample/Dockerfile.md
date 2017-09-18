After building the REST_API_Sample project into target/rest.war, you can build
the docker image by running
`docker build ./`

After building the image, test it with
`docker run -e CONNECTION_DATA=connection_string -p 8080:8080 -i -t docker_image_id`
replacing connection_string with your Context Service connection data string,
and docker_image_id with the id that you just got from running `docker build ./`
