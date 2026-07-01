package com.example.task_management_system.repository;

import com.example.task_management_system.entity.Task;
import com.example.task_management_system.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(UserEntity user);

}