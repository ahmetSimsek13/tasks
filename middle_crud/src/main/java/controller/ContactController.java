package controller;

import model.Contacts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import repository.ContactRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
public class ContactController {

    @Autowired
    private ContactRepo contactRepo;

    @GetMapping("/getAllContacts")
    public ResponseEntity<ByteArrayResource> getAllContacts() {
        List<Contacts> contactsList = new ArrayList<>(contactRepo.findAll());
        ResponseEntity.BodyBuilder responseEntityBuilder = ResponseEntity.internalServerError();
        if( !contactsList.isEmpty()) {
            return responseEntityBuilder
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "ContactList.csv")
                    .body(new ByteArrayResource(Objects.requireNonNull(SerializationUtils.serialize(contactsList))));
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/getContactById/{id}")
    public ResponseEntity<Contacts> getContactById(@PathVariable Long id) {
        Optional<Contacts> contact = contactRepo.findById(id);
        return contact.map(contacts -> new ResponseEntity<>(contacts, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/createContact")
    public ResponseEntity<Contacts> createContact(@RequestBody Contacts newContact) {
        Optional<Contacts> existing = contactRepo.findById(newContact.getId());
        if(existing.isEmpty()) {
            Contacts contactObject = contactRepo.save(newContact);
            return new ResponseEntity<>(contactObject, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/updateContactById/{id}")
    public ResponseEntity<Contacts> updateContactById(@PathVariable Long id, @RequestBody Contacts newContactData) {
        Optional<Contacts> existingContact = contactRepo.findById(id);
        if(existingContact.isPresent()) {
            Contacts updatedContactData = existingContact.get();
            updatedContactData.setCompany(newContactData.getCompany());
            updatedContactData.setEmail(newContactData.getEmail());
            updatedContactData.setName(newContactData.getName());
            updatedContactData.setSurname(newContactData.getSurname());

            Contacts contactObject = contactRepo.save(updatedContactData);
            return new ResponseEntity<>(contactObject, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/deleteContactById/{id}")
    public ResponseEntity<HttpStatus> deleteContactById(@PathVariable Long id) {
        contactRepo.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
