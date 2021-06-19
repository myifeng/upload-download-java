# upload-download-java

![size](https://img.shields.io/github/repo-size/myifeng/upload-download-java)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/myifeng/upload-download-java/Java%20CI%20with%20Gradle)
![license](https://img.shields.io/github/license/myifeng/upload-download-java)

A file upload and download module for Java.(通用性的文件上传与下载模块)

## Environments

- JDK 11

- Gradle 7.0.2

- Spring Boot 2.5.1

## Usage

- ### Upload
``` http request
POST /appendix/mydir
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundary
# File upload
Content-Disposition: form-data; name="file"; filename="demo.tar.gz"
Content-Type: application/x-gzip

# Here you specify file to upload
< ../../tar/demo.tar.gz
--WebAppBoundary--
```

- ### Download

```http request
GET /appendix/mydir/8a061b2a-bdde-489b-82e5-c3a018af9e9f/demo.tar.gz
```
## Related Efforts

- [upload-download-nodejs](https://github.com/myifeng/upload-download-nodejs) - A file upload and download module for Node.js.

## Maintainers

[@myifeng](https://github.com/myifeng).

## Contributing

Feel free to dive in! [Open an issue](https://github.com/myifeng/upload-download-java/issues/new) or submit PRs.

Standard Readme follows the [Contributor Covenant](http://contributor-covenant.org/version/1/3/0/) Code of Conduct.

## License

[MIT](LICENSE) © myifeng

