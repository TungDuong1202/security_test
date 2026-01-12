package com.example.user.converter;

import com.example.user.config.AesConfig;
import com.example.user.utils.AesUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Objects;

@Converter
@RequiredArgsConstructor
public class AccountEncryptConverter implements AttributeConverter<String, String> {
    public final SecretKey secretKey;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return Objects.isNull(attribute) ? null : AesUtil.encrypt(attribute, secretKey);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return Objects.isNull(dbData) ? null : AesUtil.decrypt(dbData, secretKey);
    }
}