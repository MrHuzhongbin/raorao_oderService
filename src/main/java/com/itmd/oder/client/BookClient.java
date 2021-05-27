package com.itmd.oder.client;

import com.itmd.api.BookServiceApi;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient("book-service")
public interface BookClient extends BookServiceApi {
}
