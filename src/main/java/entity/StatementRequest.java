package entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public record StatementRequest(
  @JsonProperty("data") List<Statement> statements,
  Integer totalPages,
  Integer totalCount,
  Integer limit,
  Integer currentPage
) implements Serializable {
}
