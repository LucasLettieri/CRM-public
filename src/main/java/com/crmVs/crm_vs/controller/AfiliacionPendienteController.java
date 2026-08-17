package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.AfiliacionPendienteDTO;
import com.crmVs.crm_vs.service.AfiliacionPendienteService;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
@RestController
@RequestMapping("/pendientes")
@Data
public class AfiliacionPendienteController {

    private final AfiliacionPendienteService afiliacionPendienteService;

    @GetMapping("/mis-pendientes")
    public List<AfiliacionPendienteDTO> listarMisPendientes(){
        return afiliacionPendienteService.listarMisPendientes();
    }
}
