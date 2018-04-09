package com.jy.quotedemo;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFunctions;

public class QuoteProducer {

    public static final String HOST = "localhost";

    public static final int PORT = 8080;
    public static final String symbol = "0001.HK";
    public static final Double[] prices = {90.25, 91.55, 93.45, 95.55, 96.25};

    private ExchangeFunction exchange = ExchangeFunctions.create(new ReactorClientHttpConnector());


    public static void main(String[] args) {
        QuoteProducer producer = new QuoteProducer();

        producer.createQuote();
        producer.printAllQuote();
    }

    public void printAllQuote() {
        try {
            System.out.println("start printAllQuote....");
            URI uri = URI.create(String.format("http://%s:%d/quote", HOST, PORT));
            ClientRequest request = ClientRequest.method(HttpMethod.GET, uri).build();

            Flux<Quote> quote = exchange.exchange(request)
                    .flatMapMany(response -> response.bodyToFlux(Quote.class));

            Mono<List<Quote>> quoteList = quote.collectList();
            System.out.println(quoteList.block());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void createQuote(){
        Arrays.asList(prices).stream().forEach(e -> this.createQuote(symbol, e));

    }

    public void createQuote(String symbol, double price) {
        try {
            System.out.printf("start createQuote..%s,%f",  symbol, price);
            URI uri = URI.create(String.format("http://%s:%d/quote", HOST, PORT));
            Quote quote = new Quote(symbol, price, System.nanoTime());


            ClientRequest request = ClientRequest.method(HttpMethod.POST, uri)
                    .body(BodyInserters.fromObject(quote)).build();

            Mono<ClientResponse> response = exchange.exchange(request);

            System.out.println(response.block().statusCode());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
