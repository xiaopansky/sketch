# Http 网络图片

翻译：[English](http.md)

## 组件

Sketch 提供了 `sketch-http-*` 系列组件以支持 Http 网络图片

| Module             | Component                                              | Android | iOS | Desktop | Web |
|:-------------------|:-------------------------------------------------------|:--------|:----|:--------|:----|
| sketch-http        | jvm: HurlHttpUriFetcher<br/>nonJvm: KtorHttpUriFetcher | ✅       | ✅   | ✅       | ✅   |
| sketch-http-hurl   | HurlHttpUriFetcher                                     | ✅       | ❌   | ✅       | ❌   |
| sketch-http-okhttp | OkHttpHttpUriFetcher                                   | ✅       | ❌   | ✅       | ❌   |
| sketch-http-ktor2  | KtorHttpUriFetcher                                     | ✅       | ✅   | ✅       | ✅   |
| sketch-http-ktor3  | KtorHttpUriFetcher                                     | ✅       | ✅   | ✅       | ✅   |

> [!IMPORTANT]
> * HurlHttpUriFetcher 使用 jvm 自带的 HttpURLConnection 实现，不需要额外的依赖
> * `sketch-http-ktor2` 和 `sketch-http-ktor3` 模块都包含各个平台所需的引擎，如果你需要使用其它引擎请使用它们的
    > core 版本，例如 `sketch-http-ktor2-core` 和 `sketch-http-ktor3-core`，然后配置自己所需的引擎的依赖

## 下载

加载网络图片前需要先从上述组件中选择一个并配置依赖，以 `sketch-http` 为例：

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (不包含 'v')

```kotlin
implementation("io.github.panpf.sketch4:sketch-http:${LAST_VERSION}")
```

> [!IMPORTANT]
> ktor2 原本不支持 wasmJs，所以 `sketch-http-ktor2` 和 `sketch-http-ktor2-core` 的 wasmJs 版本使用的其实是
`3.0.0-wasm2` 版本，而 `3.0.0-wasm2` 版本只发布到了 jetbrains 的私有仓库，所以需要你配置一下 jetbrains
> 的私有仓库，如下：
>   ```kotlin
>   allprojects {
>     repositories {
>        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")   // ktor 3.0.0-wasm2
>     }
>   }
>   ```

## 加载网络图片

直接使用 http uri 加载图片即可，如下：

```kotlin
val imageUri = "https://www.sample.com/image.jpg"

// compose
AsyncImage(
    uri = imageUri,
    contentDescription = "photo"
)

// view
imageView.loadImage(imageUri)
```

## 配置

Sketch 将 http 部分抽象为 [HttpStack]，每一个 \*HttpUriFetcher 都有对应的 [HttpStack] 实现，如下：

* HurlHttpUriFetcher：[HurlStack]
* OkHttpHttpUriFetcher：[OkHttpStack]
* KtorHttpUriFetcher：[KtorStack]

你可以先禁用相关组件的自动注册，然后在手动配置 \*HttpUriFetcher 时修改 [HttpStack] 的配置，如下：

HurlStack:

```kotlin
Sketch.Builder(context).apply {
    addIgnoreFetcherProvider(HurlHttpUriFetcherProvider::class)
    addComponents {
        val httpStack = HurlStack.Builder().apply {
            connectTimeout(5000)
            readTimeout(5000)
            userAgent("Android 8.1")
            headers("accept-encoding" to "gzip")   // 不可重复的 header
            addHeaders("cookie" to "...")    // 可重复的 header
            addInterceptor(object : HurlStack.Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val connection: HttpURLConnection = chain.connection
                    // ...
                    return chain.proceed()
                }
            })
        }.build()
        addFetcher(HurlHttpUriFetcher.Factory(httpStack))
    }
}.build()
```

OkHttpStack:

```kotlin
Sketch.Builder(context).apply {
    addIgnoreFetcherProvider(OkHttpHttpUriFetcherProvider::class)
    addComponents {
        val httpStack = OkHttpStack.Builder().apply {
            connectTimeout(5000)
            readTimeout(5000)
            userAgent("Android 8.1")
            headers("accept-encoding" to "gzip")   // 不可重复的 header
            addHeaders("cookie" to "...")    // 可重复的 header
            interceptors(object : okhttp3.Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request()
                    // ...
                    return chain.proceed(request)
                }
            })
            networkInterceptors(object : okhttp3.Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request()
                    // ...
                    return chain.proceed(request)
                }
            })
        }.build()
        addFetcher(OkHttpHttpUriFetcher.Factory(httpStack))
    }
}.build()
```

KtorStack:

```kotlin
Sketch.Builder(context).apply {
    addIgnoreFetcherProvider(KtorHttpUriFetcherProvider::class)
    addComponents {
        val httpClient = HttpClient {
            // ...
        }
        val httpStack = KtorStack(httpClient)
        addFetcher(KtorHttpUriFetcher.Factory(httpStack))
    }
}.build()
```

[comment]: <> (classs)

[version_icon]: https://img.shields.io/maven-central/v/io.github.panpf.sketch4/sketch-singleton

[version_link]: https://repo1.maven.org/maven2/io/github/panpf/sketch4/

[HttpStack]: ../../sketch-http-core/src/commonMain/kotlin/com/github/panpf/sketch/http/HttpStack.kt

[HurlStack]: ../../sketch-http-hurl/src/commonMain/kotlin/com/github/panpf/sketch/http/HurlStack.kt

[OkHttpStack]: ../../sketch-http-okhttp/src/commonMain/kotlin/com/github/panpf/sketch/http/OkHttpStack.kt

[KtorStack]: ../../sketch-http-ktor3-core/src/commonMain/kotlin/com/github/panpf/sketch/http/KtorStack.kt

[HttpUriFetcher]: ../../sketch-http-core/src/commonMain/kotlin/com/github/panpf/sketch/fetch/HttpUriFetcher.kt

[Sketch]: ../../sketch-core/src/commonMain/kotlin/com/github/panpf/sketch/Sketch.common.kt