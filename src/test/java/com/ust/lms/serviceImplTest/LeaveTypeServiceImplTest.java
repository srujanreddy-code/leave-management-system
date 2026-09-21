package com.ust.lms.serviceImplTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.DuplicateLeaveTypeException;
import com.ust.lms.common.exception.LeaveTypeNotFoundException;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.leavetype.impl.LeaveTypeServiceImpl;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveTypeServiceImpl}.
 *
 * <p>Verifies leave type creation, retrieval, pagination, updating,
 * duplicate validation, and soft deletion using mocked dependencies.</p>
 */
@ExtendWith(MockitoExtension.class)
class LeaveTypeServiceImplTest {

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @InjectMocks
    private LeaveTypeServiceImpl leaveTypeService;

    private LeaveTypeRequestDto requestDto;
    private LeaveType leaveType;
    private LeaveTypeResponseDto responseDto;

    /**
     * Sets up common test data.
     */
    @BeforeEach
    void setUp() {

        requestDto = new LeaveTypeRequestDto();
        requestDto.setName("Sick Leave");
        requestDto.setMaxDays(6);

        leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Sick Leave");
        leaveType.setMaxDays(6);
        leaveType.setDeleted(false);
        leaveType.setStatus(true);

        responseDto = new LeaveTypeResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Sick Leave");
        responseDto.setMaxDays(6);
    }

    /**
     * Verifies that a leave type is created successfully.
     */
    @Test
    void create_success() {

        Employee employee = new Employee();
        employee.setId(10L);
        employee.setJoiningDate(LocalDate.of(2026, 1, 1));
        employee.setLeaveBalance(10);
        employee.setDeleted(false);
        employee.setStatus(true);

        when(leaveTypeRepository.findByNameIgnoreCase("Sick Leave"))
                .thenReturn(Optional.empty());

        when(modelMapper.map(requestDto, LeaveType.class))
                .thenReturn(leaveType);

        when(leaveTypeRepository.save(leaveType))
                .thenReturn(leaveType);

        when(employeeRepository.findByDeletedFalse())
                .thenReturn(List.of(employee));

        when(employeeLeaveBalanceRepository.save(
                any(EmployeeLeaveBalance.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        when(employeeRepository.save(employee))
                .thenReturn(employee);

        when(modelMapper.map(
                leaveType,
                LeaveTypeResponseDto.class
        )).thenReturn(responseDto);

        LeaveTypeResponseDto result =
                leaveTypeService.create(requestDto);

        assertNotNull(result);
        assertEquals(responseDto, result);

        verify(leaveTypeRepository)
                .findByNameIgnoreCase("Sick Leave");

        verify(leaveTypeRepository)
                .save(leaveType);

        verify(employeeRepository)
                .findByDeletedFalse();

        verify(employeeLeaveBalanceRepository)
                .save(any(EmployeeLeaveBalance.class));

        verify(employeeRepository)
                .save(employee);
    }

    /**
     * Verifies that duplicate leave type names are rejected.
     */
    @Test
    void create_duplicateName_throwsDuplicateLeaveType() {

        when(leaveTypeRepository.findByNameIgnoreCase("Sick Leave"))
                .thenReturn(Optional.of(leaveType));

        assertThrows(
                DuplicateLeaveTypeException.class,
                () -> leaveTypeService.create(requestDto)
        );

        verify(leaveTypeRepository, never())
                .save(any(LeaveType.class));

        verify(employeeRepository, never())
                .findByDeletedFalse();
    }

    /**
     * Verifies that an existing leave type is retrieved successfully.
     */
    @Test
    void getById_success() {

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        when(modelMapper.map(
                leaveType,
                LeaveTypeResponseDto.class
        )).thenReturn(responseDto);

        LeaveTypeResponseDto result =
                leaveTypeService.getById(1L);

        assertNotNull(result);
        assertEquals(responseDto, result);

        verify(leaveTypeRepository)
                .findById(1L);
    }

    /**
     * Verifies that a non-existent leave type throws the expected exception.
     */
    @Test
    void getById_notFound_throwsLeaveTypeNotFound() {

        when(leaveTypeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.getById(999L)
        );
    }

    /**
     * Verifies that a soft-deleted leave type cannot be retrieved.
     */
    @Test
    void getById_softDeleted_throwsLeaveTypeNotFound() {

        leaveType.setDeleted(true);

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.getById(1L)
        );
    }

    /**
     * Verifies that all active leave types are retrieved with pagination.
     */
    @Test
    void getAll_success() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaveType> page =
                new PageImpl<>(
                        List.of(leaveType),
                        pageable,
                        1
                );

        when(leaveTypeRepository.findByDeletedFalse(pageable))
                .thenReturn(page);

        when(modelMapper.map(
                leaveType,
                LeaveTypeResponseDto.class
        )).thenReturn(responseDto);

        PageResponseDto<LeaveTypeResponseDto> result =
                leaveTypeService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        assertEquals(
                responseDto,
                result.getContent().get(0)
        );

        verify(leaveTypeRepository)
                .findByDeletedFalse(pageable);
    }

    /**
     * Verifies that a leave type is updated successfully.
     */
    @Test
    void update_success() {

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));
        doAnswer(invocation -> {
            LeaveTypeRequestDto source = invocation.getArgument(0);
            LeaveType target = invocation.getArgument(1);

            target.setName(source.getName());
            target.setMaxDays(source.getMaxDays());

            return null;
        }).when(modelMapper).map(requestDto, leaveType);

        when(leaveTypeRepository.save(leaveType))
                .thenReturn(leaveType);

        when(modelMapper.map(
                leaveType,
                LeaveTypeResponseDto.class
        )).thenReturn(responseDto);

        LeaveTypeResponseDto result =
                leaveTypeService.update(1L, requestDto);

        assertNotNull(result);
        assertEquals(responseDto, result);

        verify(modelMapper)
                .map(requestDto, leaveType);

        verify(leaveTypeRepository)
                .save(leaveType);

        verify(modelMapper)
                .map(leaveType, LeaveTypeResponseDto.class);
    }

    /**
     * Verifies that a non-existent leave type cannot be updated.
     */
    @Test
    void update_notFound_throwsLeaveTypeNotFound() {

        when(leaveTypeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.update(999L, requestDto)
        );

        verify(leaveTypeRepository, never())
                .save(any(LeaveType.class));
    }

    /**
     * Verifies that a soft-deleted leave type cannot be updated.
     */
    @Test
    void update_softDeleted_throwsLeaveTypeNotFound() {

        leaveType.setDeleted(true);

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.update(1L, requestDto)
        );
    }

    /**
     * Verifies that changing the name to an existing name is rejected.
     */
    @Test
    void update_duplicateName_throwsDuplicateLeaveType() {

        LeaveType anotherLeaveType = new LeaveType();
        anotherLeaveType.setId(2L);
        anotherLeaveType.setName("Casual Leave");
        anotherLeaveType.setMaxDays(10);

        requestDto.setName("Casual Leave");

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        when(leaveTypeRepository.findByNameIgnoreCase("Casual Leave"))
                .thenReturn(Optional.of(anotherLeaveType));

        assertThrows(
                DuplicateLeaveTypeException.class,
                () -> leaveTypeService.update(1L, requestDto)
        );

        verify(leaveTypeRepository, never())
                .save(any(LeaveType.class));
    }

    /**
     * Verifies that using the same leave type name does not trigger
     * a duplicate check.
     */
    @Test
    void update_sameName_success() {

        requestDto.setName("Sick Leave");

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        doAnswer(invocation -> {
            LeaveTypeRequestDto source = invocation.getArgument(0);
            LeaveType target = invocation.getArgument(1);

            target.setName(source.getName());
            target.setMaxDays(source.getMaxDays());

            return null;
        }).when(modelMapper).map(requestDto, leaveType);

        when(leaveTypeRepository.save(leaveType))
                .thenReturn(leaveType);

        when(modelMapper.map(
                leaveType,
                LeaveTypeResponseDto.class
        )).thenReturn(responseDto);

        LeaveTypeResponseDto result =
                leaveTypeService.update(1L, requestDto);

        assertNotNull(result);
        assertEquals(responseDto, result);

        verify(leaveTypeRepository, never())
                .findByNameIgnoreCase("Sick Leave");

        verify(modelMapper)
                .map(requestDto, leaveType);

        verify(leaveTypeRepository)
                .save(leaveType);

        verify(modelMapper)
                .map(leaveType, LeaveTypeResponseDto.class);
    }

    /**
     * Verifies that a leave type is soft deleted successfully.
     */
    @Test
    void delete_success() {

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        when(leaveTypeRepository.save(leaveType))
                .thenReturn(leaveType);

        leaveTypeService.delete(1L);

        assertEquals(true, leaveType.isDeleted());
        assertEquals(false, leaveType.isStatus());

        verify(leaveTypeRepository)
                .save(leaveType);
    }

    /**
     * Verifies that a non-existent leave type cannot be deleted.
     */
    @Test
    void delete_notFound_throwsLeaveTypeNotFound() {

        when(leaveTypeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.delete(999L)
        );

        verify(leaveTypeRepository, never())
                .save(any(LeaveType.class));
    }

    /**
     * Verifies that a soft-deleted leave type cannot be deleted again.
     */
    @Test
    void delete_softDeleted_throwsLeaveTypeNotFound() {

        leaveType.setDeleted(true);

        when(leaveTypeRepository.findById(1L))
                .thenReturn(Optional.of(leaveType));

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.delete(1L)
        );
    }
}