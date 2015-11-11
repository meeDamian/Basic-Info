## Usage


### Install `parse` and `iced`

## Open terminal #1 and

    $ cd source/ && iced -cwb -o ../parse-dist/cloud *.coffee


## Open terminal #2 and

    $ cd parse-dist/ && parse develop


## Open terminal #3 and test your POSTs there, ex:

    $ curl -X POST \
    -H "Content-Type: application/json" \
    -d "{\"key\":1,\"phone\":\"+1...\",\"vanity\":\"vanityUrl\"}"\
    https://basic-data.parseapp.com/update
