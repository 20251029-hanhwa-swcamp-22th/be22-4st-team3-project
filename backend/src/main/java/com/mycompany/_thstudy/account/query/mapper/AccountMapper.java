package com.mycompany._thstudy.account.query.mapper;

import com.mycompany._thstudy.account.query.dto.response.AccountResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {

    List<AccountResponse> findAllByUserEmail(
        @Param("userEmail") String userEmail);

    AccountResponse findByIdAndUserEmail(
        @Param("id") Long id,
        @Param("userEmail") String userEmail);
}
