import { TouchableOpacity } from "react-native";
import styled from "styled-components/native";

export const Wrapper = styled.View`
  flex: 1;
  align-items: center;
  justify-content: center;
`;

export const ButtonTitle = styled.Text`
  text-align: center;
  color: white;
  text-align: center;
`;

export const Button = styled(TouchableOpacity)`
  width: 200px;
  height: 60px;
  justify-content: center;
  align-items: center;
  padding: 5px;
  border-radius: 8px;
  background-color: blue;
`;

export const Spacer = styled.View`
  width: 200px;
  height: 30px;
`;

export const ResultContainer = styled.View`
  width: 200px;
  height: 200px;
  justify-content: center;
  align-items: center;
  padding: 5px;
  border-radius: 100px;
  border-color: blue;
  border-width: 3px;
  background-color: rgba(190, 20, 20, 0.46);
`;

export const Result = styled.Text`
  text-align: center;
  color: blue;
  text-align: center;
  font-size: 40px;
`;

export const NeedAccessLabel = styled.Text`
  text-align: center;
  color: blue;
  text-align: center;
  font-size: 11px;
`;
