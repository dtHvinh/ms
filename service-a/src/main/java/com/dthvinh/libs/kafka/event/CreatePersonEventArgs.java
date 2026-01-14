package com.dthvinh.libs.kafka.event;

import com.dthvinh.dto.CreatePersonDto;

public class CreatePersonEventArgs extends EventArgs<CreatePersonDto> {
    public CreatePersonEventArgs(CreatePersonDto createPersonDto) {
        super("CreatePersonEvent", createPersonDto);
    }
}
