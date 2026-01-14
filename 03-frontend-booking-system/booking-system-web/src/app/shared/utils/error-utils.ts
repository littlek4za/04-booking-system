export function extractFieldErrorMessage(err: any): string {
    // TODO add in error code
    let alertMessage = err.error?.message || "An unexpected error occurred.";
    if (err.error?.fieldErrorList && err.error.fieldErrorList.length > 0) {

        const status = err?.status;
        const backendMessage = err?.error?.message;

        if (backendMessage) {
            alertMessage = backendMessage;
        } else if (status) {
            alertMessage = `HTTP ${status}: An unexpected error occurred.`;
        } else {
            alertMessage = 'An unexpected error occurred.';
        }

        if (err?.error?.fieldErrorList?.length) {
            const fieldMessages = err.error.fieldErrorList
                .map((f: any) => `${f.field}:  ${f.message}`)
                .join('\n');
            alertMessage += '\n' + fieldMessages;
        }

    }
    return alertMessage;
}