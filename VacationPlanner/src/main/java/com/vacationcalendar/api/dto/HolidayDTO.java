package com.vacationcalendar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HolidayDTO {

    private List<WeekInfo> weekInfoList;
    private List<HolidayDetails> holidayDetailsList;
}
