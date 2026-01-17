package com.dthvinh.libs.kafka.event;

import com.dthvinh.dto.UpdatePersonDto;

public class UpdatePersonEventArgs extends EventArgs<UpdatePersonDto> {
    public UpdatePersonEventArgs(UpdatePersonDto dto) {
        super("UpdatePersonEvent", dto);
    }
}
