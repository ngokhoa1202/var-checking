package entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record Statement(
  UUID id,
  LocalDateTime date,
  @JsonProperty("document_no") String documentNumber,
  String amount,
  String note,
  @JsonProperty("bank_note") String bankCode,
  @JsonProperty("data_source") String dataSource
) implements Serializable {

}
