
char	*strsep(char **string_p, const char *delimiter)
{
	char		*p_str, *token;
	const char	*delimiter_p;
	  
	p_str = *string_p;
	if (p_str == 0L) {
		return 0L;
	}
  
	token = p_str;
	for (; *p_str != '\0'; p_str++) {
    
		for (delimiter_p = delimiter; *delimiter_p != '\0'; delimiter_p++) {
			if (*delimiter_p == *p_str) {
				*p_str = '\0';
				*string_p = p_str + 1;
				return token;
			}
		}
	}
  
	*string_p = 0L;
	return token;
}

char *strsep_len(char **string_p, const char *bounds_p,
		const char *delimiter, const int delimiter_len, int *len_p)
{
	char		*p_str, *token;
	const char	*delimiter_p, *loc_bounds_p, *delimiter_bounds_p;
  
	p_str = *string_p;
	if (p_str == 0L) {
		if (len_p != 0L) {
			*len_p = 0;
		}
		return 0L;
	}
  
	if (bounds_p == 0L) {
		for (loc_bounds_p = p_str; *loc_bounds_p != '\0'; loc_bounds_p++) {
		}
	}
	else {
		loc_bounds_p = bounds_p;
	}
  
	if (delimiter_len >= 0) {
		delimiter_bounds_p = delimiter + delimiter_len;
	}
	else {
		for (delimiter_bounds_p = delimiter; *delimiter_bounds_p != '\0'; delimiter_bounds_p++) {
		}
	}
  
	token = p_str;
	while (1) {
    
		if (p_str >= loc_bounds_p) {
			*string_p = 0L;
			break;
		}
    
		for (delimiter_p = delimiter; delimiter_p < delimiter_bounds_p; delimiter_p++) {
			if (*delimiter_p == *p_str) {
				*string_p = p_str + 1;
				if (len_p != 0L) {
					*len_p = p_str - token;
				}
			return token;
			}
		}
    
		p_str++;
	}
  
	if (len_p != 0L) {
		*len_p = p_str - token;
	}
	return token;
}
