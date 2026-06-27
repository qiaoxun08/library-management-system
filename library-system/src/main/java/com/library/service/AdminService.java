package com.library.service;

import com.library.entity.Admin;
import com.library.entity.Librarian;

import java.util.List;

public interface AdminService {
    // Admin account management
    List<Admin> getAllAdmins();
    Admin addAdmin(Admin admin);
    Admin updateAdmin(Admin admin);
    void deleteAdmin(Integer id);

    // Librarian account management
    List<Librarian> getAllLibrarians();
    Librarian addLibrarian(Librarian librarian);
    Librarian updateLibrarian(Librarian librarian);
    void deleteLibrarian(Integer id);
}
