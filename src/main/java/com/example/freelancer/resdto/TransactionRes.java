package com.example.freelancer.resdto;

import com.example.freelancer.dto.TransactionHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRes {
    private List<TransactionHistoryDTO> list;
    private int totalPage;
}
