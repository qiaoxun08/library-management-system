package com.library.service;

import com.library.entity.Reader;
import java.util.List;

public interface ReaderService {
    List<Reader> getAllReaders();
    Reader getReaderById(Integer id);
    Reader getReaderByReaderId(String readerId);
    Reader addReader(Reader reader);
    Reader updateReader(Reader reader);
    void deleteReader(Integer id);
    void payFine(Integer id);
    void changePassword(String readerId, String oldPassword, String newPassword);
    void resetPassword(String readerId, String newPassword);
    Reader updateProfile(String readerId, Reader profileData);
}
