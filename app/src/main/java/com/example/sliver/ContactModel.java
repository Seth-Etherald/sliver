package com.example.sliver;

public class ContactModel {
  private String name, status, image;

  public ContactModel() {}

  public ContactModel(String name, String status, String image) {
    this.name = name;
    this.status = status;
    this.image = image;
  }

  public String getName() {
    return name;
  }

  public String getStatus() {
    return status;
  }

  public String getImage() {
    return image;
  }
}
