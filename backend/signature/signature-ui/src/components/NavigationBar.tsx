/* eslint-disable @typescript-eslint/no-empty-object-type */
import React from 'react';
import {useNavigate} from "react-router";

interface NavigationBarProps {
}

const NavigationBar: React.FC<NavigationBarProps> = (props) => {
    const navigate = useNavigate()
    return (
        <div>
            <button onClick={() => navigate('/templates')}>Templates</button>
            <button onClick={() => navigate('/documents')}>Documents</button>
        </div>
    )
};

export default NavigationBar;
