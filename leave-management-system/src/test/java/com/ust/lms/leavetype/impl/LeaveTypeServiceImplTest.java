package com.ust.lms.leavetype.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.LeaveTypeRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveTypeServiceImplTest {

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private LeaveTypeServiceImpl leaveTypeService;

    private LeaveType leaveType;

    @BeforeEach
    void setUp() {
        leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Casual Leave");
        leaveType.setMaxDays(12);
        leaveType.setDeleted(false);
    }

    @Test
    void create_success() {
        LeaveTypeRequestDto dto = new LeaveTypeRequestDto();
        dto.setName("Casual Leave");
        dto.setMaxDays(12);

        when(leaveTypeRepository.findByNameIgnoreCase("Casual Leave")).thenReturn(Optional.empty());
        when(leaveTypeRepository.save(any(LeaveType.class))).thenReturn(leaveType);

        LeaveTypeResponseDto result = leaveTypeService.create(dto);

        assertNotNull(result);
        assertEquals("Casual Leave", result.getName());
    }

    @Test
    void create_duplicateName_throwsBadRequest() {
        LeaveTypeRequestDto dto = new LeaveTypeRequestDto();
        dto.setName("Casual Leave");

        when(leaveTypeRepository.findByNameIgnoreCase("Casual Leave")).thenReturn(Optional.of(leaveType));

        assertThrows(BadRequestException.class, () -> leaveTypeService.create(dto));
    }

    @Test
    void getById_success() {
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        LeaveTypeResponseDto result = leaveTypeService.getById(1L);

        assertNotNull(result);
        assertEquals("Casual Leave", result.getName());
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(leaveTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaveTypeService.getById(99L));
    }

    @Test
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaveType> page = new PageImpl<>(List.of(leaveType), pageable, 1);
        when(leaveTypeRepository.findByDeletedFalse(pageable)).thenReturn(page);

        PageResponseDto<LeaveTypeResponseDto> result = leaveTypeService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_duplicateName_throwsBadRequest() {
        LeaveTypeRequestDto dto = new LeaveTypeRequestDto();
        dto.setName("Sick Leave");

        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.findByNameIgnoreCase("Sick Leave")).thenReturn(Optional.of(new LeaveType()));

        assertThrows(BadRequestException.class, () -> leaveTypeService.update(1L, dto));
    }

    @Test
    void delete_success() {
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        leaveTypeService.delete(1L);

        assertTrue(leaveType.isDeleted());
        verify(leaveTypeRepository, times(1)).save(leaveType);
    }
}