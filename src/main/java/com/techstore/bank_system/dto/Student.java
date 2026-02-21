package com.techstore.bank_system.dto;

import java.util.Objects;

public class Student {
    private int id;
    private String fullname;
    private int age;

    public Student() {
    }

    public Student(int id, String fullname, int age) {
        this.id = id;
        this.fullname = fullname;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", fullname='" + fullname + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && age == student.age && Objects.equals(fullname, student.fullname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullname, age);
    }
}

