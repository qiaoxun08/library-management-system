package com.library.service.impl;

import com.library.entity.Reader;
import com.library.exception.BusinessException;
import com.library.mapper.ReaderMapper;
import com.library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReaderServiceImpl implements ReaderService {

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Reader> getAllReaders() {
        return readerMapper.findAll();
    }

    @Override
    public Reader getReaderById(Integer id) {
        return readerMapper.findById(id);
    }

    @Override
    public Reader getReaderByReaderId(String readerId) {
        return readerMapper.findByReaderId(readerId);
    }

    @Override
    public Reader addReader(Reader reader) {
        // Encode password before saving
        if (reader.getPassword() != null) {
            reader.setPassword(passwordEncoder.encode(reader.getPassword()));
        }
        readerMapper.insert(reader);
        return reader;
    }

    @Override
    public Reader updateReader(Reader reader) {
        // Encode password if it's being updated
        if (reader.getPassword() != null && !reader.getPassword().isEmpty()) {
            reader.setPassword(passwordEncoder.encode(reader.getPassword()));
        }
        readerMapper.update(reader);
        return reader;
    }

    @Override
    public void deleteReader(Integer id) {
        readerMapper.delete(id);
    }

    @Override
    public void payFine(Integer id) {
        Reader reader = readerMapper.findById(id);
        if (reader == null) {
            throw new BusinessException("读者不存在");
        }
        // 使用原子操作清除罚款，避免并发竞态
        if (reader.getFineAmount() != null && reader.getFineAmount().compareTo(BigDecimal.ZERO) > 0) {
            readerMapper.decrementFineAmount(id, reader.getFineAmount());
        }
    }

    @Override
    public void changePassword(String readerId, String oldPassword, String newPassword) {
        Reader reader = readerMapper.findByReaderId(readerId);
        if (reader == null) {
            throw new BusinessException("读者不存在");
        }
        if (!passwordEncoder.matches(oldPassword, reader.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        reader.setPassword(passwordEncoder.encode(newPassword));
        readerMapper.update(reader);
    }

    @Override
    public void resetPassword(String readerId, String newPassword) {
        Reader reader = readerMapper.findByReaderId(readerId);
        if (reader == null) {
            throw new BusinessException("读者不存在");
        }
        reader.setPassword(passwordEncoder.encode(newPassword));
        readerMapper.update(reader);
    }

    @Override
    public Reader updateProfile(String readerId, Reader profileData) {
        Reader existing = readerMapper.findByReaderId(readerId);
        if (existing == null) {
            throw new BusinessException("读者不存在");
        }
        // 白名单：只允许读者自行修改基本资料字段
        if (profileData.getRealName() != null) existing.setRealName(profileData.getRealName());
        if (profileData.getGender() != null) existing.setGender(profileData.getGender());
        if (profileData.getPhone() != null) existing.setPhone(profileData.getPhone());
        if (profileData.getEmail() != null) existing.setEmail(profileData.getEmail());
        if (profileData.getDepartment() != null) existing.setDepartment(profileData.getDepartment());
        if (profileData.getLanguage() != null) existing.setLanguage(profileData.getLanguage());
        // 不允许修改: status, maxBorrowCount, fineAmount, currentBorrowCount, password, readerId
        readerMapper.update(existing);
        return existing;
    }
}
