package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.BorrowerDto;
import java.util.List;

public interface BorrowerService {
    BorrowerDto addBorrower(BorrowerDto borrowerDto);

    List<BorrowerDto> getAllBorrowers();

    BorrowerDto getBorrowerById(Long id);
}
