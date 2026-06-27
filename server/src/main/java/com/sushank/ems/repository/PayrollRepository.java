package com.sushank.ems.repository;

import com.sushank.ems.entity.Payroll;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends MongoRepository<Payroll, String> {
    Optional<Payroll> findByEmployeeIdAndMonthYear(String employeeId, String monthYear);
    List<Payroll> findByEmployeeId(String employeeId);
}
