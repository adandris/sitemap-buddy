# Sitemap Buddy

*Sitemap Buddy* is a modern sitemap generator. Written in pure Java, it is platform-independent, fast and vertically scalable.

## Usage

Build the project with Maven:
```
> mvn package
```

Basic usage after the project was built:

```
> java -jar target/sitemap-buddy-1.0-SNAPSHOT.jar -u https://www.teodorstoev.com -o sitemap.xml
```

## Technical details

The implementation is based on:

- [Vert.x](http://vertx.io/)
- [Jsoup](https://jsoup.org/)
- [JAXB](https://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding)