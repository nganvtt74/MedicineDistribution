package com.example.medicinedistribution.BUS.Interface;

import java.util.List;

public interface BaseBUS<T,id> {

    boolean insert(T t);

    boolean update(T t);

    boolean delete(id id);

    T findById(id id);

    List<T> findAll();
}
