package com.example.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class PaginationService {

    private static final int BAR_LENGTH = 5;

    // totalPages = 전체 페이지 크기
    public List<Integer> getPaginationBarNumbers(int currentNumber, int totalPages) {
        int startNumber = Math.max(currentNumber - BAR_LENGTH / 2, 0);
        int endNumber = Math.min(startNumber + BAR_LENGTH, totalPages);
        return IntStream.range(startNumber, endNumber).boxed().toList();
    }

    public int currentBarLength() {
        return BAR_LENGTH;
    }
}
