import { JWT } from '@aws-amplify/core';

export interface ElnJwtPayload extends JWT {
  family_name: string;
  given_name: string;
}
