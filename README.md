# api-limiter

## Overview

A thread safe Java library to limit client access to APIs in a certain timeframe.   
It can be configured to single APIs or to root APIs and it can limit access to    
specific clients or to all clients.    


## Use cases
One can use this library in order to limit client access to:
- a traditional java method
- REST API 
- SOAP services

## License

Apache License, Version 2.0

## Maven

```xml
<dependency>
    <groupId>com.dinuberinde</groupId>
    <artifactId>api-limiter</artifactId>
    <version>1.4</version>
</dependency>
```

## Usage
 
#### Specific clients

Example to configure some APIs with some clients.   
The library will bock the configured clients for the configured apis.

```java
// register the api name, the max calls, the timeframe and the client
ApiLimiter.registerApis(
        new ApiConfig("/api/my-api-1", 100, 30 * 1000, "client-1"),
        new ApiConfig("/api/my-api-2", 500, 60 * 1000, "client-2")
);

....
        
// consume the api in some part of your code on behalf of client-1
if (!ApiLimiter.consume("/api/my-api-1", "client-1")) {
    throw new MyCustomException(String.format("%s blocked for too many requests when accessing %s", "client-1", "/api/my-api-1"));
}

```
Example to configure a single API with some clients.   
The library will bock the configured clients for the configured api.

```java
// register the api name, the max calls, the timeframe and the clients
ApiLimiter.registerApis(ApiConfig.of(
            "/api/my-api",
            500, 
            30 * 1000,
            "client-1",
            "client-2",
            "client-3"
        )
);

....
        
// consume the api in some part of your code on behalf of client-1
if (!ApiLimiter.consume("/api/my-api", "client-1")) {
    throw new MyCustomException(String.format("%s blocked for too many requests when accessing %s", "client-1", "/api/my-api"));
}
```

Example to configure a root API with some clients in order to consume any child api of the root api.  
The library will block the configured clients that will consume a child api of the root api. It is important to add the * symbol at the end of the api name in order to identify the root api name.

```java
// register the api name, the max calls, the timeframe and the clients
// the library will configure for each client the root api 
ApiLimiter.registerApis(ApiConfig.of(
            "/api/my-root-api/*",
            500, 
            30 * 1000,
            "client-1",
            "client-2",
            "client-3"
        )
);

....
        
// consume the api in some part of your code on behalf of client-1
if (!ApiLimiter.consume("/api/my-root-api/test-me", "client-1")) {
    throw new MyCustomException(String.format("%s blocked for too many requests when accessing %s", "client-1", "/api/my-root-api/test-me"));
}
```

#### All clients

Example to configure some APIs which can be consumed by any client.   
The library will bock any client for the configured apis.

```java
// register the api name, the max calls and the timeframe
ApiLimiter.registerApis(
        new ApiConfig("/api/my-api-1", 100, 30 * 1000),
        new ApiConfig("/api/my-api-2", 500, 60 * 1000)
);

....
        
// consume the api in some part of your code on behalf of any client
if (!ApiLimiter.consume("/api/my-api-1")) {
    throw new MyCustomException(String.format("Received too many requests for API %s", "/api/my-api-1"));
}
```

Example to configure some root APIs which can be consumed by any client.  
The library will block any client that will consume a child api of the root api. It is important to add the * symbol at the end of the api name in order to identify the root api name.

```java
// register the api name, the max calls and the timeframe
ApiLimiter.registerApis(
        new ApiConfig("/api/my-root-api-1/*", 100, 30 * 1000),
        new ApiConfig("/api/my-root-api-2/*", 500, 60 * 1000)
);

....
        
// consume the api in some part of your code on behalf of any client
if (!ApiLimiter.consume("/api/my-root-api-1/test")) {
    throw new MyCustomException(String.format("Received too many requests for API %s", "/api/my-root-api-1/test"));
}
```

## Author
Dinu Berinde <dinu2193@gmail.com>

