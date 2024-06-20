package workdayspreadsheettoics;

public class Faculty {
    private String name;
    private String email;
    private String status;
    private String facultyCategory;
    private String phone;
    private String office;
    private String department;
    private String imageUrl;
    private String id;

    public Faculty(String name, String email, String status, String facultyCategory, String phone, String office, String department, String imageUrl, String id) {
        this.name = name;
        this.email = email;
        this.status = status;
        this.facultyCategory = facultyCategory;
        this.phone = phone;
        this.office = office;
        this.department = department;
        this.imageUrl = imageUrl;
        this.id = id;
    }
    public Faculty() {
        
    }

    public String toString() {
        return "Name: " + name + "\nEmail: " + email + "\nStatus: " + status + "\nFaculty Category: " + facultyCategory + "\nPhone: " + phone + "\nOffice: " + office + "\nDepartment: " + department + "\nImage URL: " + imageUrl + "\nID(s): " + id;
    }
    public String exportStringForFile() {
        return name + "|" + email + "|" + status + "|" + facultyCategory + "|" + phone + "|" + office + "|" + department + "|" + imageUrl + "|" + id;
    }

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getStatus() {
        return status;
    }
    public String getfacultyCategory() {
        return facultyCategory;
    }
    public String getPhone() {
        return phone;
    }
    public String getOffice() {
        return office;
    }
    public String getDepartment() {
        return department;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getId() {
        return id;
    }

}
