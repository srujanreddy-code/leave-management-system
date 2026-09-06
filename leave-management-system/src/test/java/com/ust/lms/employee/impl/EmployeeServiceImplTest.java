package com.ust.lms.employee.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private User user;
    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");

        department = new Department();
        department.setId(10L);
        department.setName("Engineering");

        employee = new Employee();
        employee.setId(100L);
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(LocalDate.now());
        employee.setLeaveBalance(20);
        employee.setDeleted(false);
    }

    @Test
    void create_success() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(1L);
        dto.setDepartmentId(10L);
        dto.setJoiningDate(LocalDate.now());
        dto.setLeaveBalance(20);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDto result = employeeService.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Engineering", result.getDepartmentName());
    }

    @Test
    void create_userNotFound_throwsResourceNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.create(dto));
    }

    @Test
    void create_departmentNotFound_throwsResourceNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(1L);
        dto.setDepartmentId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.create(dto));
    }

    @Test
    void getById_success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        EmployeeResponseDto result = employeeService.getById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getUserName());
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(99L));
    }

    @Test
    void getById_softDeletedRecord_throwsResourceNotFound() {
        employee.setDeleted(true);
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(100L));
    }

    @Test
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(page);

        PageResponseDto<EmployeeResponseDto> result = employeeService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getUserName());
    }

    @Test
    void getByDepartmentId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(10L, pageable)).thenReturn(page);

        PageResponseDto<EmployeeResponseDto> result = employeeService.getByDepartmentId(10L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Engineering", result.getContent().get(0).getDepartmentName());
    }

    @Test
    void getIdByUserId_success() {
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));

        Long employeeId = employeeService.getIdByUserId(1L);

        assertEquals(100L, employeeId);
    }

    @Test
    void getIdByUserId_notFound_throwsResourceNotFound() {
        when(employeeRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getIdByUserId(99L));
    }

    @Test
    void update_success() {
        Department newDept = new Department();
        newDept.setId(20L);
        newDept.setName("HR");

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(20L);
        dto.setLeaveBalance(25);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(newDept));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeResponseDto result = employeeService.update(100L, dto);

        assertNotNull(result);
        assertEquals("HR", result.getDepartmentName());
    }

    @Test
    void delete_success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.delete(100L);

        assertTrue(employee.isDeleted());
        verify(employeeRepository, times(1)).save(employee);
    }
}