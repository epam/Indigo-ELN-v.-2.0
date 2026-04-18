export const fileTypeConfig = {
  doc: {
    mimeTypes: ['application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
    extensions: '.doc,.docx,',
  },
  image: {
    mimeTypes: ['image/png', 'image/jpeg'],
    extensions: '.jpg, .jpeg, .png,',
  },
  pdf: {
    mimeTypes: ['application/pdf'],
    extensions: '.pdf,',
  },
  xls: {
    mimeTypes: ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'],
    extensions: '.xls, .xlsx,',
  },
  ppt: {
    mimeTypes: [
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      'application/vnd.ms-powerpoint',
    ],
    extensions: '.ppt, .pptx,',
  },
  csv: {
    mimeTypes: ['text/csv'],
    extensions: '.csv,',
  },
};
