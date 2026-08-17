package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.dto.AfiliacionPendienteDTO;
import com.crmVs.crm_vs.model.AfiliacionPendiente;
import com.crmVs.crm_vs.repository.AfiliacionPendienteRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
public class AfiliacionPendienteService {

    private final LeadService leadService;
    private final AfiliacionPendienteRepository afiliacionPendienteRepository;

    public List<AfiliacionPendienteDTO> listarMisPendientes(){
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();


        return afiliacionPendienteRepository
                .findByLead_VendedorIdAndLead_TenantId(usuarioLogueado.getUserId(), usuarioLogueado.getTenantId())
                .stream()
                .map(this::convertirPendienteADTO)
                .toList();
    }

    public AfiliacionPendienteDTO convertirPendienteADTO(AfiliacionPendiente afiliacionPendiente){
        AfiliacionPendienteDTO dto = new AfiliacionPendienteDTO();
        dto.setLead(    leadService.convertirADTO(afiliacionPendiente.getLead())   );
        dto.setProximoRecordatorio(afiliacionPendiente.getProximoRecordatorio());
        dto.setFechaConfirmacion(afiliacionPendiente.getFechaConfirmacion());
        return dto;
    }
}
