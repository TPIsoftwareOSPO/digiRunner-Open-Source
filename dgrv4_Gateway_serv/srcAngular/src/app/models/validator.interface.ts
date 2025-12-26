export interface ValidatorFormat {
    field: string;
    type: string;
    isRequired?: {
        value: boolean;
        msg: string;
    };
    maxLength?: {
        value: number;
        msg: string;
    };
    max?: {
        value: number;
        msg: string;
    }
    minLength?: {
        value: number;
        msg: string;
    };
    min?: {
        value: number;
        msg: string;
    }
    pattern?: {
        value: string;
        msg: string;
    };
}
