package com.library.service.impl;

import com.library.entity.Admin;
import com.library.entity.Librarian;
import com.library.mapper.AdminMapper;
import com.library.mapper.LibrarianMapper;
import com.library.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private LibrarianMapper librarianMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Admin> getAllAdmins() {
        return adminMapper.findAll();
    }

    @Override
    public Admin addAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        adminMapper.insert(admin);
        return admin;
    }

    @Override
    public Admin updateAdmin(Admin admin) {
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            admin.setPassword(null);
        }
        adminMapper.update(admin);
        return admin;
    }

    @Override
    public void deleteAdmin(Integer id) {
        adminMapper.delete(id);
    }

    @Override
    public List<Librarian> getAllLibrarians() {
        return librarianMapper.findAll();
    }

    @Override
    public Librarian addLibrarian(Librarian librarian) {
        librarian.setPassword(passwordEncoder.encode(librarian.getPassword()));
        librarianMapper.insert(librarian);
        return librarian;
    }

    @Override
    public Librarian updateLibrarian(Librarian librarian) {
        if (librarian.getPassword() != null && !librarian.getPassword().isEmpty()) {
            librarian.setPassword(passwordEncoder.encode(librarian.getPassword()));
        } else {
            librarian.setPassword(null);
        }
        librarianMapper.update(librarian);
        return librarian;
    }

    @Override
    public void deleteLibrarian(Integer id) {
        librarianMapper.delete(id);
    }
}
