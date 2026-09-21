package com.ust.lms.leavetype.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.DuplicateLeaveTypeException;
import com.ust.lms.common.exception.LeaveTypeNotFoundException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.leavetype.LeaveTypeService;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import java.time.LocalDate;
import java.util.List;


/**
 * Service implementation for managing leave types and their associated
 * employee leave balances.
 */
@Service
@Slf4j
public class LeaveTypeServiceImpl extends CommonService implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;
    private final EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    public LeaveTypeServiceImpl(LeaveTypeRepository leaveTypeRepository, ModelMapper modelMapper, EmployeeRepository employeeRepository, EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.modelMapper = modelMapper;
        this.employeeRepository = employeeRepository;
        this.employeeLeaveBalanceRepository = employeeLeaveBalanceRepository;
    }

    /**
     * Creates a new leave type and initializes its leave balance for all active employees.
     *
     * @param dto leave type details
     * @return response containing the created leave type
     * @throws DuplicateLeaveTypeException if a leave type with the same name already exists
     */
    @Override
    public LeaveTypeResponseDto create(LeaveTypeRequestDto dto) {
        log.info("Creating leave type with name: {}", dto.getName());
        leaveTypeRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
            throw new DuplicateLeaveTypeException("Leave type name already exists");
        });

        LeaveType leaveType = modelMapper.map(dto, LeaveType.class);
        setAuditFields(leaveType, true);
        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Leave type created successfully with id: {}", saved.getId());

        List<Employee> employees = employeeRepository.findByDeletedFalse();

        for (Employee employee : employees) {

            EmployeeLeaveBalance balance = new EmployeeLeaveBalance();
            balance.setEmployee(employee);
            balance.setLeaveType(saved);
            balance.setBalance(saved.getMaxDays());

            LocalDate leaveYearStart = employee.getJoiningDate();
            LocalDate leaveYearEnd = leaveYearStart.plusYears(1).minusDays(1);

            balance.setLeaveYearStart(leaveYearStart);
            balance.setLeaveYearEnd(leaveYearEnd);

            setAuditFields(balance, true);

            employeeLeaveBalanceRepository.save(balance);

            employee.setLeaveBalance(employee.getLeaveBalance() + saved.getMaxDays());
            employeeRepository.save(employee);
        }

        return modelMapper.map(saved, LeaveTypeResponseDto.class);
    }

    /**
     * Retrieves a leave type by its ID.
     *
     * @param id leave type ID
     * @return response containing the requested leave type
     * @throws LeaveTypeNotFoundException if the leave type does not exist or has been deleted
     */
    @Override
    public LeaveTypeResponseDto getById(Long id) {
        log.info("Fetching leave type with id: {}", id);
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .filter(lt -> !lt.isDeleted())
                .orElseThrow(() -> new LeaveTypeNotFoundException("LeaveType not found"));
        return modelMapper.map(leaveType, LeaveTypeResponseDto.class);
    }

    /**
     * Retrieves all active leave types using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing leave types
     */
    @Override
    public PageResponseDto<LeaveTypeResponseDto> getAll(Pageable pageable) {
        Page<LeaveType> result = leaveTypeRepository.findByDeletedFalse(pageable);
        return new PageResponseDto<>(
                result.getContent().stream().map(lt -> modelMapper.map(lt, LeaveTypeResponseDto.class)).toList(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * Updates an existing leave type.
     *
     * @param id leave type ID
     * @param dto updated leave type details
     * @return response containing the updated leave type
     * @throws LeaveTypeNotFoundException if the leave type does not exist or has been deleted
     * @throws DuplicateLeaveTypeException if another leave type with the same name already exists
     */
    @Override
    public LeaveTypeResponseDto update(Long id, LeaveTypeRequestDto dto) {
        log.info("Updating leave type with id: {}", id);
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .filter(lt -> !lt.isDeleted())
                .orElseThrow(() -> new LeaveTypeNotFoundException("LeaveType not found"));
        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(leaveType.getName())) {
            leaveTypeRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
                throw new DuplicateLeaveTypeException("Leave type name already exists");
            });
        }

        modelMapper.map(dto, leaveType);
        setAuditFields(leaveType, false);
        LeaveType updated = leaveTypeRepository.save(leaveType);
        log.info("Leave type updated successfully with id: {}", id);
        return modelMapper.map(updated, LeaveTypeResponseDto.class);
    }

    /**
     * Soft deletes an existing leave type.
     *
     * @param id leave type ID
     * @throws LeaveTypeNotFoundException if the leave type does not exist or has already been deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Deleting leave type with id: {}", id);
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .filter(lt -> !lt.isDeleted())
                .orElseThrow(() -> new LeaveTypeNotFoundException("LeaveType not found"));
        softDelete(leaveType);
        setAuditFields(leaveType, false);
        leaveTypeRepository.save(leaveType);
        log.info("Leave type deleted successfully with id: {}", id);
    }
}