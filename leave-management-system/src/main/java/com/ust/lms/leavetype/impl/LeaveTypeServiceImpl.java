package com.ust.lms.leavetype.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.leavetype.LeaveTypeService;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.LeaveTypeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LeaveTypeServiceImpl extends CommonService implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final ModelMapper modelMapper;

    public LeaveTypeServiceImpl(LeaveTypeRepository leaveTypeRepository, ModelMapper modelMapper) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public LeaveTypeResponseDto create(LeaveTypeRequestDto dto) {
        leaveTypeRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
            throw new BadRequestException("Leave type name already exists");
        });

        LeaveType leaveType = modelMapper.map(dto, LeaveType.class);
        setAuditFields(leaveType, true);
        LeaveType saved = leaveTypeRepository.save(leaveType);
        return modelMapper.map(saved, LeaveTypeResponseDto.class);
    }

    @Override
    public LeaveTypeResponseDto getById(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .filter(lt -> !lt.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType not found"));
        return modelMapper.map(leaveType, LeaveTypeResponseDto.class);
    }

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

    @Override
    public LeaveTypeResponseDto update(Long id, LeaveTypeRequestDto dto) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .filter(lt -> !lt.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType not found"));

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(leaveType.getName())) {
            leaveTypeRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
                throw new BadRequestException("Leave type name already exists");
            });
        }

        modelMapper.map(dto, leaveType);
        setAuditFields(leaveType, false);
        LeaveType updated = leaveTypeRepository.save(leaveType);
        return modelMapper.map(updated, LeaveTypeResponseDto.class);
    }

    @Override
    public void delete(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .filter(lt -> !lt.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType not found"));
        softDelete(leaveType);
        setAuditFields(leaveType, false);
        leaveTypeRepository.save(leaveType);
    }
}