/**
 * Utility function to download a blob as a file
 * @param blob - The blob data to download
 * @param filename - The name of the file to download (defaults to 'download')
 */
export function downloadBlob(blob: Blob, filename = 'download'): void {
  // Validate the blob
  if (!blob || !(blob instanceof Blob)) {
    console.error('Invalid blob provided for download:', filename);
    return;
  }

  // Check if the blob is empty
  if (blob.size === 0) {
    console.warn('Empty blob provided for download:', filename);
    return;
  }
  // Create a link and trigger download
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  a.remove();
}
