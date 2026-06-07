package com.gestionmorgue.service;

import com.gestionmorgue.dao.FamilyContactDao;
import com.gestionmorgue.model.FamilyContact;
import com.gestionmorgue.model.Deceased;

import java.util.List;

public class FamilyContactService {

    private final FamilyContactDao dao;

    public FamilyContactService() {
        this.dao = new FamilyContactDao();
    }

    public FamilyContact createContact(Deceased deceased, String fullName, String relationship,
                                        String phone, String email, String address, String notes) {
        FamilyContact contact = new FamilyContact();
        contact.setDeceased(deceased);
        contact.setFullName(fullName);
        contact.setRelationship(relationship);
        contact.setPhone(phone);
        contact.setEmail(email);
        contact.setAddress(address);
        contact.setNotes(notes);
        return dao.save(contact);
    }

    public List<FamilyContact> getContactsForDeceased(Long deceasedId) {
        return dao.findByDeceasedId(deceasedId);
    }

    public void deleteContact(FamilyContact contact) {
        dao.delete(contact);
    }
}
