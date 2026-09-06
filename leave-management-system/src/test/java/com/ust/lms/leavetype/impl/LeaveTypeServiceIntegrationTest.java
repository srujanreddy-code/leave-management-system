package com.ust.lms.leavetype.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.leavetype.LeaveTypeService;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LeaveTypeServiceIntegrationTest {

    @Autowired
    private LeaveTypeService leaveTypeService;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @BeforeEach
    void setUp() {
        leaveTypeRepository.deleteAll();
    }

    @Test
    void create_shouldCreateLeaveType() {
        LeaveTypeRequestDto dto = createRequest("Annual Leave", 20);

        LeaveTypeResponseDto response = leaveTypeService.create(dto);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Annual Leave", response.getName());
        assertEquals(20, response.getMaxDays());
    }

    @Test
    void create_shouldThrowWhenNameAlreadyExists() {
        leaveTypeService.create(createRequest("Annual Leave", 20));

        LeaveTypeRequestDto dto = createRequest("Annual Leave", 15);

        assertThrows(
                BadRequestException.class,
                () -> leaveTypeService.create(dto)
        );
    }

    @Test
    void create_shouldThrowWhenNameExistsWithDifferentCase() {
        leaveTypeService.create(createRequest("Annual Leave", 20));

        LeaveTypeRequestDto dto = createRequest("annual leave", 15);

        assertThrows(
                BadRequestException.class,
                () -> leaveTypeService.create(dto)
        );
    }

    @Test
    void getById_shouldReturnLeaveType() {
        LeaveTypeResponseDto created =
                leaveTypeService.create(createRequest("Annual Leave", 20));

        LeaveTypeResponseDto response =
                leaveTypeService.getById(created.getId());

        assertNotNull(response);
        assertEquals(created.getId(), response.getId());
        assertEquals("Annual Leave", response.getName());
        assertEquals(20, response.getMaxDays());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.getById(999L)
        );
    }

    @Test
    void getById_shouldThrowWhenDeleted() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        leaveType.setDeleted(true);
        leaveTypeRepository.save(leaveType);

        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.getById(leaveType.getId())
        );
    }

    @Test
    void getAll_shouldReturnOnlyNonDeletedLeaveTypes() {
        LeaveType leaveType1 = createLeaveType("Annual Leave", 20);
        createLeaveType("Sick Leave", 10);

        leaveType1.setDeleted(true);
        leaveTypeRepository.save(leaveType1);

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveTypeResponseDto> response =
                leaveTypeService.getAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Sick Leave", response.getContent().get(0).getName());
    }

    @Test
    void getAll_shouldReturnMultipleLeaveTypes() {
        createLeaveType("Annual Leave", 20);
        createLeaveType("Sick Leave", 10);
        createLeaveType("Casual Leave", 12);

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveTypeResponseDto> response =
                leaveTypeService.getAll(pageable);

        assertEquals(3, response.getContent().size());
        assertEquals(3, response.getTotalElements());
    }

    @Test
    void update_shouldUpdateLeaveType() {
        LeaveTypeResponseDto created =
                leaveTypeService.create(createRequest("Annual Leave", 20));

        LeaveTypeRequestDto dto =
                createRequest("Updated Annual Leave", 25);

        LeaveTypeResponseDto response =
                leaveTypeService.update(created.getId(), dto);

        assertEquals(created.getId(), response.getId());
        assertEquals("Updated Annual Leave", response.getName());
        assertEquals(25, response.getMaxDays());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        LeaveTypeRequestDto dto =
                createRequest("Annual Leave", 20);

        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.update(999L, dto)
        );
    }

    @Test
    void update_shouldThrowWhenDeleted() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        leaveType.setDeleted(true);
        leaveTypeRepository.save(leaveType);

        LeaveTypeRequestDto dto =
                createRequest("Updated Leave", 25);

        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.update(leaveType.getId(), dto)
        );
    }

    @Test
    void update_shouldThrowWhenNameAlreadyExists() {
        LeaveType first = createLeaveType("Annual Leave", 20);
        createLeaveType("Sick Leave", 10);

        LeaveTypeRequestDto dto =
                createRequest("Sick Leave", 15);

        assertThrows(
                BadRequestException.class,
                () -> leaveTypeService.update(first.getId(), dto)
        );
    }

    @Test
    void update_shouldThrowWhenNameExistsWithDifferentCase() {
        LeaveType first = createLeaveType("Annual Leave", 20);
        createLeaveType("Sick Leave", 10);

        LeaveTypeRequestDto dto =
                createRequest("sick leave", 15);

        assertThrows(
                BadRequestException.class,
                () -> leaveTypeService.update(first.getId(), dto)
        );
    }

    @Test
    void update_shouldAllowSameName() {
        LeaveTypeResponseDto created =
                leaveTypeService.create(createRequest("Annual Leave", 20));

        LeaveTypeRequestDto dto =
                createRequest("Annual Leave", 25);

        LeaveTypeResponseDto response =
                leaveTypeService.update(created.getId(), dto);

        assertEquals("Annual Leave", response.getName());
        assertEquals(25, response.getMaxDays());
    }

    @Test
    void update_shouldAllowSameNameWithDifferentCase() {
        LeaveTypeResponseDto created =
                leaveTypeService.create(createRequest("Annual Leave", 20));

        LeaveTypeRequestDto dto =
                createRequest("annual leave", 25);

        LeaveTypeResponseDto response =
                leaveTypeService.update(created.getId(), dto);

        assertEquals("annual leave", response.getName());
        assertEquals(25, response.getMaxDays());
    }

    @Test
    void delete_shouldSoftDeleteLeaveType() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        leaveTypeService.delete(leaveType.getId());

        LeaveType deleted =
                leaveTypeRepository.findById(leaveType.getId()).orElseThrow();

        assertTrue(deleted.isDeleted());
    }

    @Test
    void delete_shouldMakeLeaveTypeUnavailable() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        leaveTypeService.delete(leaveType.getId());

        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.getById(leaveType.getId())
        );
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.delete(999L)
        );
    }

    @Test
    void delete_shouldThrowWhenAlreadyDeleted() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        leaveType.setDeleted(true);
        leaveTypeRepository.save(leaveType);

        assertThrows(
                ResourceNotFoundException.class,
                () -> leaveTypeService.delete(leaveType.getId())
        );
    }

    private LeaveTypeRequestDto createRequest(String name, int maxDays) {
        LeaveTypeRequestDto dto = new LeaveTypeRequestDto();
        dto.setName(name);
        dto.setMaxDays(maxDays);
        return dto;
    }

    private LeaveType createLeaveType(String name, int maxDays) {
        LeaveType leaveType = new LeaveType();
        leaveType.setName(name);
        leaveType.setMaxDays(maxDays);
        leaveType.setDeleted(false);
        leaveType.setStatus(true);
        return leaveTypeRepository.save(leaveType);
    }
}