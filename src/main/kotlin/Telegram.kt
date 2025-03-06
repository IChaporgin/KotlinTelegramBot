import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates"
    val builder = HttpClient.newBuilder()
    val client: HttpClient = builder.build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    println(response.body())
    val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
    println(responseGetUpdates.body())
}