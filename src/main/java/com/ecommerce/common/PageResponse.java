package com.ecommerce.common;

import java.util.List;
import lombok.Data;

@Data
public class PageResponse<T> {
  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean last;
  private boolean first;
}
